import {useState, useRef, useCallback, useEffect} from 'react';
import {ScenarioValidationService} from '../../services/scandallShuffle/ScenarioValidationService';

type ValidationStatus = 'idle' | 'checking' | 'available' | 'unavailable' | 'too-short' | 'error';

interface NameValidationState {
    status: ValidationStatus;
    message: string | null;
}

/**
 * Hook for real-time, debounced scenario name validation.
 * @param initialName The initial name to validate.
 * @param excludeScenarioId The ID of the scenario to exclude from the check (for editing).
 */
export function useScenarioNameValidation(initialName: string = '', excludeScenarioId?: string) {
    const [name, setName] = useState(initialName);
    const [state, setState] = useState<NameValidationState>({
        status: 'idle',
        message: null,
    });

    const debounceTimer = useRef<NodeJS.Timeout | null>(null);

    const checkNameAvailability = useCallback(async (nameToCheck: string) => {
        const trimmedName = nameToCheck.trim();

        if (trimmedName.length > 0 && trimmedName.length < 3) {
            setState({status: 'too-short', message: 'Name must be at least 3 characters.'});
            return;
        }

        if (trimmedName.length < 3) {
            setState({status: 'idle', message: null});
            return;
        }

        setState({status: 'checking', message: 'Checking availability...'});

        try {
            const nameExists = await ScenarioValidationService.checkScenarioNameExists(trimmedName, excludeScenarioId);

            if (nameExists) {
                setState({status: 'unavailable', message: 'This name is already taken.'});
            } else {
                setState({status: 'available', message: 'This name is available!'});
            }
        } catch (error) {
            console.error('Error during name validation:', error);
            setState({status: 'error', message: 'Could not verify name.'});
        }
    }, [excludeScenarioId]);

    const handleNameChange = useCallback((newName: string) => {
        setName(newName);
        if (debounceTimer.current) {
            clearTimeout(debounceTimer.current);
        }

        setState({status: 'idle', message: null});

        debounceTimer.current = setTimeout(() => {
            checkNameAvailability(newName).catch(console.error);
        }, 500); // 500ms delay after user stops typing
    }, [checkNameAvailability]);

    useEffect(() => {
        // Clean up the timer when the component unmounts
        return () => {
            if (debounceTimer.current) {
                clearTimeout(debounceTimer.current);
            }
        };
    }, []);

    return {
        name,
        setName: handleNameChange,
        validationStatus: state.status,
        validationMessage: state.message,
    };
}
